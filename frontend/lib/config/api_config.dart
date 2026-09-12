// Point this at your Spring Boot backend.
// Use 10.0.2.2 instead of localhost when testing on the Android emulator.
class ApiConfig {
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/api',
  );
}
