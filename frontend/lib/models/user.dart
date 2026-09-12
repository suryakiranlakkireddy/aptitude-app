class AppUser {
  final int id;
  final String fullName;
  final String role;

  AppUser({required this.id, required this.fullName, required this.role});

  factory AppUser.fromAuthJson(Map<String, dynamic> json) => AppUser(
        id: json['userId'],
        fullName: json['fullName'],
        role: json['role'],
      );
}
