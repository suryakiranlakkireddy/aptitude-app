class TopicProgress {
  final String topicName;
  final int totalAttempts;
  final int totalCorrect;
  final int totalWrong;
  final double completionPercent;
  final int currentStreakDays;

  TopicProgress({
    required this.topicName,
    required this.totalAttempts,
    required this.totalCorrect,
    required this.totalWrong,
    required this.completionPercent,
    required this.currentStreakDays,
  });

  factory TopicProgress.fromJson(Map<String, dynamic> json) => TopicProgress(
        topicName: json['topicName'],
        totalAttempts: json['totalAttempts'],
        totalCorrect: json['totalCorrect'],
        totalWrong: json['totalWrong'],
        completionPercent: (json['completionPercent'] as num).toDouble(),
        currentStreakDays: json['currentStreakDays'],
      );
}
