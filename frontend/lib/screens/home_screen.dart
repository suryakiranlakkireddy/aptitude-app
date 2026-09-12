import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';
import '../models/topic.dart';
import '../models/subscription.dart';
import 'quiz_list_screen.dart';
import 'progress_screen.dart';
import 'subscription_screen.dart';
import 'profile_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});
  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _api = ApiService();
  int _tab = 0;

  @override
  Widget build(BuildContext context) {
    final pages = [
      _TopicsTab(api: _api),
      const ProgressScreen(),
      const SubscriptionScreen(),
      const ProfileScreen(),
    ];
    return Scaffold(
      body: pages[_tab],
      bottomNavigationBar: NavigationBar(
        selectedIndex: _tab,
        onDestinationSelected: (i) => setState(() => _tab = i),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.menu_book), label: 'Topics'),
          NavigationDestination(icon: Icon(Icons.bar_chart), label: 'Progress'),
          NavigationDestination(icon: Icon(Icons.workspace_premium), label: 'Plan'),
          NavigationDestination(icon: Icon(Icons.person), label: 'Profile'),
        ],
      ),
    );
  }
}

class _TopicsTab extends StatefulWidget {
  final ApiService api;
  const _TopicsTab({required this.api});
  @override
  State<_TopicsTab> createState() => _TopicsTabState();
}

class _TopicsTabState extends State<_TopicsTab> {
  List<Topic> _topics = [];
  SubscriptionInfo? _sub;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final topicsJson = await widget.api.get('/admin/topics');
      final subJson = await widget.api.get('/subscription/me');
      setState(() {
        _topics = (topicsJson as List).map((t) => Topic.fromJson(t)).toList();
        _sub = SubscriptionInfo.fromJson(subJson);
        _loading = false;
      });
    } catch (_) {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            const Text('Topics', style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            if (_sub != null && !_sub!.active)
              Card(
                color: Colors.amber.shade50,
                child: const Padding(
                  padding: EdgeInsets.all(12),
                  child: Text('No active plan. Subscribe (Rs 59 / 4 months) to unlock all quizzes and tests.'),
                ),
              ),
            const SizedBox(height: 12),
            if (_topics.isEmpty) const Text('No topics yet. Admin needs to add some.'),
            for (final topic in _topics)
              Card(
                child: ListTile(
                  leading: const Icon(Icons.category, color: Color(0xFF2E5AAC)),
                  title: Text(topic.name),
                  subtitle: topic.description != null ? Text(topic.description!) : null,
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.push(context,
                      MaterialPageRoute(builder: (_) => QuizListScreen(topic: topic))),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
