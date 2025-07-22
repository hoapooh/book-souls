// Install NuGet package: FirebaseAdmin

using FirebaseAdmin;
using FirebaseAdmin.Messaging;
using Google.Apis.Auth.OAuth2;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

public class FCMService
{
  private readonly ILogger<FCMService> _logger;
  private readonly FirebaseMessaging _messaging;

  public FCMService(ILogger<FCMService> logger)
  {
    _logger = logger;

    // Initialize Firebase Admin SDK (do this once in Startup.cs)
    if (FirebaseApp.DefaultInstance == null)
    {
      FirebaseApp.Create(new AppOptions()
      {
        Credential = GoogleCredential.FromFile("path/to/your/firebase-service-account-key.json")
      });
    }

    _messaging = FirebaseMessaging.DefaultInstance;
  }

  /// <summary>
  /// Send order status notification to a specific user
  /// </summary>
  public async Task<string> SendOrderStatusNotificationAsync(string userId, string orderId, string newStatus, string userFCMToken)
  {
    var message = new Message()
    {
      Notification = new Notification()
      {
        Title = "Order Status Updated",
        Body = $"Your order #{orderId} is now {newStatus}"
      },
      Data = new Dictionary<string, string>()
            {
                { "type", "order_status_update" },
                { "orderId", orderId },
                { "status", newStatus },
                { "userId", userId }
            },
      Token = userFCMToken
    };

    try
    {
      var response = await _messaging.SendAsync(message);
      _logger.LogInformation($"Successfully sent message: {response}");
      return response;
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, "Error sending FCM message");
      throw;
    }
  }

  /// <summary>
  /// Send notification to multiple users (e.g., all admins for new orders)
  /// </summary>
  public async Task<BatchResponse> SendMulticastNotificationAsync(List<string> fcmTokens, string title, string body, Dictionary<string, string> data = null)
  {
    var message = new MulticastMessage()
    {
      Notification = new Notification()
      {
        Title = title,
        Body = body
      },
      Data = data ?? new Dictionary<string, string>(),
      Tokens = fcmTokens
    };

    try
    {
      var response = await _messaging.SendMulticastAsync(message);
      _logger.LogInformation($"Successfully sent multicast message. Success: {response.SuccessCount}, Failure: {response.FailureCount}");

      // Log individual failures
      if (response.FailureCount > 0)
      {
        for (int i = 0; i < response.Responses.Count; i++)
        {
          if (!response.Responses[i].IsSuccess)
          {
            _logger.LogWarning($"Failed to send to token {fcmTokens[i]}: {response.Responses[i].Exception?.Message}");
          }
        }
      }

      return response;
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, "Error sending multicast FCM message");
      throw;
    }
  }

  /// <summary>
  /// Send notification to a topic (e.g., all users subscribed to "orders")
  /// </summary>
  public async Task<string> SendTopicNotificationAsync(string topic, string title, string body, Dictionary<string, string> data = null)
  {
    var message = new Message()
    {
      Notification = new Notification()
      {
        Title = title,
        Body = body
      },
      Data = data ?? new Dictionary<string, string>(),
      Topic = topic
    };

    try
    {
      var response = await _messaging.SendAsync(message);
      _logger.LogInformation($"Successfully sent topic message to {topic}: {response}");
      return response;
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, $"Error sending FCM message to topic {topic}");
      throw;
    }
  }
}

// Data models for your application
public class Order
{
  public string Id { get; set; }
  public string UserId { get; set; }
  public string Status { get; set; }
  public DateTime CreatedAt { get; set; }
  public DateTime UpdatedAt { get; set; }
}

public class User
{
  public string Id { get; set; }
  public string Email { get; set; }
  public string FCMToken { get; set; }
  public string DeviceId { get; set; }
}

// Service class that combines order management with notifications
public class OrderService
{
  private readonly FCMService _fcmService;
  private readonly ILogger<OrderService> _logger;
  // Inject your database context or repository here
  // private readonly ApplicationDbContext _dbContext;

  public OrderService(FCMService fcmService, ILogger<OrderService> logger)
  {
    _fcmService = fcmService;
    _logger = logger;
  }

  /// <summary>
  /// Update order status and send notification
  /// </summary>
  public async Task<bool> UpdateOrderStatusAsync(string orderId, string newStatus, bool notifyUser = true)
  {
    try
    {
      // 1. Update order in database
      var order = await GetOrderByIdAsync(orderId);
      if (order == null)
      {
        _logger.LogWarning($"Order {orderId} not found");
        return false;
      }

      order.Status = newStatus;
      order.UpdatedAt = DateTime.UtcNow;
      await UpdateOrderInDatabaseAsync(order);

      // 2. Send notification if requested
      if (notifyUser)
      {
        var user = await GetUserByIdAsync(order.UserId);
        if (user != null && !string.IsNullOrEmpty(user.FCMToken))
        {
          await _fcmService.SendOrderStatusNotificationAsync(
              user.Id,
              orderId,
              newStatus,
              user.FCMToken
          );
        }
        else
        {
          _logger.LogWarning($"User {order.UserId} not found or has no FCM token");
        }
      }

      _logger.LogInformation($"Order {orderId} updated to {newStatus}");
      return true;
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, $"Error updating order status for {orderId}");
      return false;
    }
  }

  // Mock methods - replace with your actual database operations
  private async Task<Order> GetOrderByIdAsync(string orderId)
  {
    // Replace with your database query
    // return await _dbContext.Orders.FindAsync(orderId);
    throw new NotImplementedException("Implement your database query here");
  }

  private async Task<User> GetUserByIdAsync(string userId)
  {
    // Replace with your database query
    // return await _dbContext.Users.FindAsync(userId);
    throw new NotImplementedException("Implement your database query here");
  }

  private async Task UpdateOrderInDatabaseAsync(Order order)
  {
    // Replace with your database update
    // _dbContext.Orders.Update(order);
    // await _dbContext.SaveChangesAsync();
    throw new NotImplementedException("Implement your database update here");
  }
}
