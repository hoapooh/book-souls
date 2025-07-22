using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Authorization;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;

[ApiController]
[Route("api/[controller]")]
[Authorize] // Add authentication as needed
public class OrdersController : ControllerBase
{
  private readonly OrderService _orderService;
  private readonly FCMService _fcmService;
  private readonly ILogger<OrdersController> _logger;

  public OrdersController(OrderService orderService, FCMService fcmService, ILogger<OrdersController> logger)
  {
    _orderService = orderService;
    _fcmService = fcmService;
    _logger = logger;
  }

  /// <summary>
  /// Update order status and optionally send push notification
  /// </summary>
  [HttpPut("{orderId}/status")]
  public async Task<IActionResult> UpdateOrderStatus(string orderId, [FromBody] UpdateOrderStatusRequest request)
  {
    try
    {
      if (string.IsNullOrEmpty(orderId) || request == null || string.IsNullOrEmpty(request.Status))
      {
        return BadRequest("Invalid order ID or status");
      }

      var success = await _orderService.UpdateOrderStatusAsync(orderId, request.Status, request.NotifyUser);

      if (success)
      {
        return Ok(new
        {
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

  /// <summary>
  /// Send custom notification to a user
  /// </summary>
  [HttpPost("notify-user")]
  public async Task<IActionResult> NotifyUser([FromBody] NotifyUserRequest request)
  {
    try
    {
      // Get user's FCM token from database
      var user = await GetUserWithFCMTokenAsync(request.UserId);
      if (user == null || string.IsNullOrEmpty(user.FCMToken))
      {
        return BadRequest("User not found or has no FCM token");
      }

      // Send custom notification
      var response = await _fcmService.SendOrderStatusNotificationAsync(
          request.UserId,
          request.OrderId ?? "N/A",
          request.Message,
          user.FCMToken
      );

      return Ok(new { message = "Notification sent", response = response });
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, "Error sending custom notification");
      return StatusCode(500, "Internal server error");
    }
  }

  /// <summary>
  /// Test FCM notification endpoint
  /// </summary>
  [HttpPost("test-notification")]
  public async Task<IActionResult> TestNotification([FromBody] TestNotificationRequest request)
  {
    try
    {
      var message = new Message()
      {
        Notification = new Notification()
        {
          Title = request.Title ?? "Test Notification",
          Body = request.Body ?? "This is a test notification from your server"
        },
        Data = new Dictionary<string, string>()
                {
                    { "type", "test" },
                    { "timestamp", DateTime.UtcNow.ToString() }
                },
        Token = request.FCMToken
      };

      var response = await FirebaseMessaging.DefaultInstance.SendAsync(message);
      return Ok(new { message = "Test notification sent", response = response });
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, "Error sending test notification");
      return StatusCode(500, ex.Message);
    }
  }

  // Helper method - implement based on your database
  private async Task<User> GetUserWithFCMTokenAsync(string userId)
  {
    // Replace with your database query
    // return await _dbContext.Users.FindAsync(userId);
    throw new NotImplementedException("Implement your database query here");
  }
}

// Request models
public class UpdateOrderStatusRequest
{
  public string Status { get; set; }
  public bool NotifyUser { get; set; } = true;
}

public class NotifyUserRequest
{
  public string UserId { get; set; }
  public string OrderId { get; set; }
  public string Message { get; set; }
}

public class TestNotificationRequest
{
  public string FCMToken { get; set; }
  public string Title { get; set; }
  public string Body { get; set; }
}
