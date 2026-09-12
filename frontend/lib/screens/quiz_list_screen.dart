import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/topic.dart';
import '../models/quiz.dart';
import 'quiz_play_screen.dart';

class QuizListScreen extends StatefulWidget {
  final Topic topic;
  const QuizListScreen({super.key, required this.topic});

  @override
  State<QuizListScreen> createState() => _QuizListScreenState();
}

class _QuizListScreenState extends State<QuizListScreen> {
  final _api = ApiService();
  List<Quiz> _quizzes = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final json = await _api.get('/quizzes?topicId=${widget.topic.id}');
      setState(() {
        _quizzes = (json as List).map((q) => Quiz.fromJson(q)).toList();
        _loading = false;
      });
    } catch (_) {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.topic.name)),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _quizzes.isEmpty
              ? const Center(child: Text('No quizzes in this topic yet.'))
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _quizzes.length,
                  itemBuilder: (context, i) {
                    final quiz = _quizzes[i];
                    return Card(
                      child: ListTile(
                        leading: Icon(quiz.mockTest ? Icons.timer : Icons.quiz, color: const Color(0xFF2E5AAC)),
                        title: Text(quiz.title),
                        subtitle: Text('${quiz.durationMinutes} min${quiz.mockTest ? ' - Mock test' : ''}'),
                        trailing: const Icon(Icons.play_arrow),
                        onTap: () => Navigator.push(context,
                            MaterialPageRoute(builder: (_) => QuizPlayScreen(quizId: quiz.id))),
                      ),
                    );
                  },
                ),
    );
  }
}
