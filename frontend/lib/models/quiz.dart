class QuestionOption {
  final int id;
  final String optionText;

  QuestionOption({required this.id, required this.optionText});

  factory QuestionOption.fromJson(Map<String, dynamic> json) =>
      QuestionOption(id: json['id'], optionText: json['optionText']);
}

class QuizQuestion {
  final int id;
  final String questionText;
  final List<QuestionOption> options;

  QuizQuestion({required this.id, required this.questionText, required this.options});

  factory QuizQuestion.fromJson(Map<String, dynamic> json) => QuizQuestion(
        id: json['id'],
        questionText: json['questionText'],
        options: (json['options'] as List)
            .map((o) => QuestionOption.fromJson(o))
            .toList(),
      );
}

class Quiz {
  final int id;
  final String title;
  final String? description;
  final String topicName;
  final int durationMinutes;
  final bool mockTest;
  final List<QuizQuestion>? questions;

  Quiz({
    required this.id,
    required this.title,
    this.description,
    required this.topicName,
    required this.durationMinutes,
    required this.mockTest,
    this.questions,
  });

  factory Quiz.fromJson(Map<String, dynamic> json) => Quiz(
        id: json['id'],
        title: json['title'],
        description: json['description'],
        topicName: json['topicName'],
        durationMinutes: json['durationMinutes'] ?? 20,
        mockTest: json['mockTest'] ?? false,
        questions: json['questions'] == null
            ? null
            : (json['questions'] as List).map((q) => QuizQuestion.fromJson(q)).toList(),
      );
}
