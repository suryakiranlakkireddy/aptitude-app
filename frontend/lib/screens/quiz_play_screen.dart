import 'dart:async';
import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/quiz.dart';
import 'result_screen.dart';

class QuizPlayScreen extends StatefulWidget {
  final int quizId;
  const QuizPlayScreen({super.key, required this.quizId});

  @override
  State<QuizPlayScreen> createState() => _QuizPlayScreenState();
}

class _QuizPlayScreenState extends State<QuizPlayScreen> {
  final _api = ApiService();
  Quiz? _quiz;
  bool _loading = true;
  int _index = 0;
  final Map<int, int> _answers = {}; // questionId -> optionId
  int _secondsElapsed = 0;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final json = await _api.get('/quizzes/${widget.quizId}');
    setState(() {
      _quiz = Quiz.fromJson(json);
      _loading = false;
    });
    _timer = Timer.periodic(const Duration(seconds: 1), (_) {
      setState(() => _secondsElapsed++);
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  Future<void> _submit() async {
    _timer?.cancel();
    final res = await _api.post('/attempts/submit', body: {
      'quizId': widget.quizId,
      'answers': _answers.map((k, v) => MapEntry(k.toString(), v)),
      'timeTakenSeconds': _secondsElapsed,
    });
    if (!mounted) return;
    Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => ResultScreen(
      totalQuestions: res['totalQuestions'],
      correctCount: res['correctCount'],
      scorePercent: (res['scorePercent'] as num).toDouble(),
    )));
  }

  @override
  Widget build(BuildContext context) {
    if (_loading || _quiz == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    final questions = _quiz!.questions ?? [];
    if (questions.isEmpty) {
      return Scaffold(appBar: AppBar(title: Text(_quiz!.title)), body: const Center(child: Text('No questions yet.')));
    }
    final q = questions[_index];
    final minutes = (_secondsElapsed ~/ 60).toString().padLeft(2, '0');
    final seconds = (_secondsElapsed % 60).toString().padLeft(2, '0');

    return Scaffold(
      appBar: AppBar(
        title: Text(_quiz!.title),
        actions: [Padding(
          padding: const EdgeInsets.only(right: 16),
          child: Center(child: Text('$minutes:$seconds')),
        )],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            LinearProgressIndicator(value: (_index + 1) / questions.length),
            const SizedBox(height: 8),
            Text('Question ${_index + 1} of ${questions.length}', style: const TextStyle(color: Colors.grey)),
            const SizedBox(height: 12),
            Text(q.questionText, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
            const SizedBox(height: 16),
            Expanded(
              child: ListView(
                children: q.options.map((opt) {
                  final selected = _answers[q.id] == opt.id;
                  return Card(
                    color: selected ? const Color(0xFFE3ECFB) : null,
                    child: RadioListTile<int>(
                      value: opt.id,
                      groupValue: _answers[q.id],
                      title: Text(opt.optionText),
                      onChanged: (v) => setState(() => _answers[q.id] = v!),
                    ),
                  );
                }).toList(),
              ),
            ),
            Row(
              children: [
                if (_index > 0)
                  Expanded(child: OutlinedButton(
                    onPressed: () => setState(() => _index--),
                    child: const Text('Previous'),
                  )),
                if (_index > 0) const SizedBox(width: 12),
                Expanded(child: FilledButton(
                  onPressed: _index == questions.length - 1 ? _submit : () => setState(() => _index++),
                  child: Text(_index == questions.length - 1 ? 'Submit' : 'Next'),
                )),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
