/* 
   COMPLETE FLOW EXAMPLE
   
   This shows how the web client updates order status without knowing FCM tokens
*/

// 1. WEB CLIENT (Frontend JavaScript)
// The web client only needs to send order ID and new status
function updateOrderStatus(orderId, newStatus) {
    fetch(`/api/orders/${orderId}/status`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getAuthToken()}`
        },
        body: JSON.stringify({
            status: newStatus,
            notifyUser: true  // This tells server to send push notification
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('Order updated successfully:', data);
        alert(`Order ${orderId} updated to ${newStatus}`);
    })
    .catch(error => {
        console.error('Error updating order:', error);
    });
}

// 2. SERVER BACKEND (.NET)
// Server handles everything - gets FCM token from database and sends notification

[HttpPut("{orderId}/status")]
public async Task<IActionResult> UpdateOrderStatus(string orderId, [FromBody] UpdateOrderStatusRequest request)
{
    try
    {
        // Server does all the work:
        // 1. Updates order in database
        // 2. Gets user's FCM token from database  
        // 3. Sends push notification to mobile app
        
        var success = await _orderService.UpdateOrderStatusAsync(orderId, request.Status, request.NotifyUser);
        
        if (success)
        {
            return Ok(new { 
                message = $"Order {orderId} updated to {request.Status}", 
                notificationSent = request.NotifyUser 
            });
        }
        else
        {
            return NotFound($"Order {orderId} not found");
        }
    }
    catch (Exception ex)
    {
        _logger.LogError(ex, $"Error updating order {orderId}");
        return StatusCode(500, "Internal server error");
    }
}

// 3. DATABASE STRUCTURE
/*
   Orders Table:
   - Id (string)
   - UserId (string) ← Links to Users table
   - Status (string)
   - CreatedAt (datetime)
   - UpdatedAt (datetime)
   
   Users Table:
   - Id (string)
   - Email (string)
   - FCMToken (string) ← Stored when mobile app logs in
   - DeviceId (string)
   - TokenUpdatedAt (datetime)
*/

// 4. COMPLETE FLOW:
/*
   Step 1: Admin opens website, sees order list
   Step 2: Admin clicks "Update Status" → Changes order from "Processing" to "Shipped"
   Step 3: Website calls: PUT /api/orders/123/status { "status": "shipped", "notifyUser": true }
   Step 4: Server:
           a) Updates order status in database
           b) Finds the order's userId
           c) Looks up user's FCM token in database
           d) Sends FCM notification to that token
   Step 5: Mobile app receives notification: "Order Status Updated: Your order #123 is now shipped"
   Step 6: User taps notification → App opens orders page
*/

// 5. NO FCM TOKEN NEEDED FROM WEB CLIENT
/*
   The beauty of this approach:
   - Web client doesn't need to know anything about FCM tokens
   - Web client doesn't need to know which mobile devices to notify
   - Server handles all FCM logic internally
   - Mobile app automatically receives notifications
   
   Web client only needs to provide:
   - Order ID (which order to update)
   - New status (what to change it to)
   - Whether to notify user (optional flag)
*/
