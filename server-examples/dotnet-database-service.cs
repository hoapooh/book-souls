// Example: Database service to get FCM token
public class UserRepository
{
  private readonly IDbContext _dbContext; // Your database context

  public UserRepository(IDbContext dbContext)
  {
    _dbContext = dbContext;
  }

  /// <summary>
  /// Get user's FCM token by user ID
  /// </summary>
  public async Task<string> GetUserFCMTokenAsync(string userId)
  {
    var user = await _dbContext.Users
        .Where(u => u.Id == userId)
        .Select(u => u.FCMToken)
        .FirstOrDefaultAsync();

    return user;
  }

  /// <summary>
  /// Get user's FCM token by order ID
  /// </summary>
  public async Task<string> GetFCMTokenByOrderIdAsync(string orderId)
  {
    var fcmToken = await _dbContext.Orders
        .Where(o => o.Id == orderId)
        .Join(_dbContext.Users, o => o.UserId, u => u.Id, (o, u) => u.FCMToken)
        .FirstOrDefaultAsync();

    return fcmToken;
  }

  /// <summary>
  /// Get all active FCM tokens for multiple users
  /// </summary>
  public async Task<List<string>> GetFCMTokensForUsersAsync(List<string> userIds)
  {
    var tokens = await _dbContext.Users
        .Where(u => userIds.Contains(u.Id) && !string.IsNullOrEmpty(u.FCMToken))
        .Select(u => u.FCMToken)
        .ToListAsync();

    return tokens;
  }
}

// Updated order service using database lookup
public class OrderService
{
  private readonly FCMService _fcmService;
  private readonly UserRepository _userRepository;
  private readonly ILogger<OrderService> _logger;

  public OrderService(FCMService fcmService, UserRepository userRepository, ILogger<OrderService> logger)
  {
    _fcmService = fcmService;
    _userRepository = userRepository;
    _logger = logger;
  }

  /// <summary>
  /// Update order status - no FCM token needed from client
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
        // Get FCM token from database using order ID
        var fcmToken = await _userRepository.GetFCMTokenByOrderIdAsync(orderId);

        if (!string.IsNullOrEmpty(fcmToken))
        {
          await _fcmService.SendOrderStatusNotificationAsync(
              order.UserId,
              orderId,
              newStatus,
              fcmToken
          );
          _logger.LogInformation($"Notification sent for order {orderId}");
        }
        else
        {
          _logger.LogWarning($"No FCM token found for order {orderId}");
        }
      }

      return true;
    }
    catch (Exception ex)
    {
      _logger.LogError(ex, $"Error updating order status for {orderId}");
      return false;
    }
  }

  // Your existing methods...
  private async Task<Order> GetOrderByIdAsync(string orderId) { /* Implementation */ }
  private async Task UpdateOrderInDatabaseAsync(Order order) { /* Implementation */ }
}
