import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/progress.dart';

// Shows only the current user's own progress - the backend scopes /progress/me
// strictly to the authenticated user, so no cross-user data ever appears here.
class ProgressScreen extends StatefulWidget {
  const ProgressScreen({super.key});
  @override
  State<ProgressScreen> createState() => _ProgressScreenState();
}

class _ProgressScreenState extends State<ProgressScreen> {
  final _api = ApiService();
  List<TopicProgress> _progress = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final json = await _api.get('/progress/me');
      setState(() {
        _progress = (json as List).map((p) => TopicProgress.fromJson(p)).toList();
        _loading = false;
      });
    } catch (_) {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: _loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _load,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  const Text('My Progress', style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 16),
                  if (_progress.isEmpty) const Text('Attempt a quiz to start tracking your progress.'),
                  for (final p in _progress)
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(p.topicName, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                            const SizedBox(height: 8),
                            LinearProgressIndicator(value: p.completionPercent / 100),
                            const SizedBox(height: 8),
                            Text('Accuracy: ${p.completionPercent.toStringAsFixed(1)}% - '
                                '${p.totalAttempts} attempts - Streak: ${p.currentStreakDays} days'),
                          ],
                        ),
                      ),
                    ),
                ],
              ),
            ),
    );
  }
}
