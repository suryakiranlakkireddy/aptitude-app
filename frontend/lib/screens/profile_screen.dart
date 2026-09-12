import 'package:flutter/material.dart';
import '../services/auth_service.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});
  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _auth = AuthService();
  String? _name;
  String? _role;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final name = await _auth.getFullName();
    final role = await _auth.getRole();
    setState(() { _name = name; _role = role; });
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text('Profile', style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
            const SizedBox(height: 24),
            const CircleAvatar(radius: 40, child: Icon(Icons.person, size: 40)),
            const SizedBox(height: 16),
            Center(child: Text(_name ?? '', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold))),
            Center(child: Text(_role ?? '', style: const TextStyle(color: Colors.grey))),
            const SizedBox(height: 32),
            OutlinedButton.icon(
              icon: const Icon(Icons.logout),
              label: const Text('Log out'),
              onPressed: () async {
                await _auth.logout();
                if (context.mounted) Navigator.pushNamedAndRemoveUntil(context, '/login', (r) => false);
              },
            ),
          ],
        ),
      ),
    );
  }
}
