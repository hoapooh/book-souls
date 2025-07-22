using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

public class Startup
{
  public Startup(IConfiguration configuration)
  {
    Configuration = configuration;
  }

  public IConfiguration Configuration { get; }

  public void ConfigureServices(IServiceCollection services)
  {
    services.AddControllers();

    // Add CORS if needed for web client
    services.AddCors(options =>
    {
      options.AddPolicy("AllowAll",
              builder =>
              {
              builder
                      .AllowAnyOrigin()
                      .AllowAnyMethod()
                      .AllowAnyHeader();
            });
    });

    // Initialize Firebase Admin SDK
    InitializeFirebase();

    // Register your services
    services.AddScoped<FCMService>();
    services.AddScoped<OrderService>();

    // Add database context (Entity Framework example)
    // services.AddDbContext<ApplicationDbContext>(options =>
    //     options.UseSqlServer(Configuration.GetConnectionString("DefaultConnection")));

    // Add authentication if needed
    // services.AddAuthentication("Bearer")
    //     .AddJwtBearer("Bearer", options => { ... });

    services.AddLogging();
  }

  public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
  {
    if (env.IsDevelopment())
    {
      app.UseDeveloperExceptionPage();
    }

    app.UseRouting();
    app.UseCors("AllowAll");

    // app.UseAuthentication();
    // app.UseAuthorization();

    app.UseEndpoints(endpoints =>
    {
      endpoints.MapControllers();
    });
  }

  private void InitializeFirebase()
  {
    try
    {
      if (FirebaseApp.DefaultInstance == null)
      {
        // Option 1: Use service account file
        FirebaseApp.Create(new AppOptions()
        {
          Credential = GoogleCredential.FromFile("firebase-service-account-key.json")
        });

        // Option 2: Use environment variable
        // Environment.SetEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", "path/to/service-account.json");
        // FirebaseApp.Create();

        // Option 3: Use service account JSON from configuration
        // var serviceAccountJson = Configuration["Firebase:ServiceAccountJson"];
        // FirebaseApp.Create(new AppOptions()
        // {
        //     Credential = GoogleCredential.FromJson(serviceAccountJson)
        // });
      }
    }
    catch (Exception ex)
    {
      Console.WriteLine($"Error initializing Firebase: {ex.Message}");
      throw;
    }
  }
}
