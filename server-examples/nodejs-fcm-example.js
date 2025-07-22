// Install: npm install firebase-admin

const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
const serviceAccount = require("./path/to/your/firebase-service-account-key.json");

admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
});

// Function to send push notification
async function sendOrderStatusNotification(userId, orderId, newStatus, userFCMToken) {
	const message = {
		notification: {
			title: "Order Status Updated",
			body: `Your order #${orderId} is now ${newStatus}`,
		},
		data: {
			type: "order_status_update",
			orderId: orderId,
			status: newStatus,
			userId: userId,
		},
		token: userFCMToken, // The FCM token from the user's device
	};

	try {
		const response = await admin.messaging().send(message);
		console.log("Successfully sent message:", response);
		return response;
	} catch (error) {
		console.log("Error sending message:", error);
		throw error;
	}
}

// Example usage when order status is updated
async function updateOrderStatus(orderId, newStatus) {
	try {
		// 1. Update order in database
		await updateOrderInDatabase(orderId, newStatus);

		// 2. Get user info and FCM token
		const order = await getOrderById(orderId);
		const user = await getUserById(order.userId);

		if (user.fcmToken) {
			// 3. Send push notification
			await sendOrderStatusNotification(user.id, orderId, newStatus, user.fcmToken);
		}

		console.log(`Order ${orderId} updated to ${newStatus} and notification sent`);
	} catch (error) {
		console.error("Error updating order status:", error);
	}
}

module.exports = {
	sendOrderStatusNotification,
	updateOrderStatus,
};
