class Topic {
  final int id;
  final String name;
  final String? description;

  Topic({required this.id, required this.name, this.description});

  factory Topic.fromJson(Map<String, dynamic> json) => Topic(
        id: json['id'],
        name: json['name'],
        description: json['description'],
      );
}
