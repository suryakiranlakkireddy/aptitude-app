import 'package:flutter/material.dart';
import '../services/api_service.dart';
import '../models/subscription.dart';

class SubscriptionScreen extends StatefulWidget {
  const SubscriptionScreen({super.key});
  @override
  State<SubscriptionScreen> createState() => _SubscriptionScreenState();
}

class _SubscriptionScreenState extends State<SubscriptionScreen> {
  final _api = ApiService();
  SubscriptionInfo? _sub;
  bool _loading = true;
  bool _purchasing = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final json = await _api.get('/subscription/me');
    setState(() {
      _sub = SubscriptionInfo.fromJson(json);
      _loading = false;
    });
  }

  // TODO: wire this up to a real payment gateway (e.g. Razorpay) before launch.
  // Today it calls the backend directly, which records the payment as successful.
  Future<void> _purchase() async {
    setState(() => _purchasing = true);
    try {
      final json = await _api.post('/subscription/purchase');
      setState(() => _sub = SubscriptionInfo.fromJson(json));
    } finally {
      if (mounted) setState(() => _purchasing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator());
    final active = _sub?.active ?? false;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text('My Plan', style: TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
            const SizedBox(height: 16),
            Card(
              color: active ? Colors.green.shade50 : Colors.red.shade50,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(active ? 'Active plan' : 'No active plan',
                        style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                    if (_sub?.expiresAt != null)
                      Text('Valid until: ${_sub!.expiresAt!.toLocal().toString().split(' ').first}'),
                    const SizedBox(height: 8),
                    const Text('Full access - Rs 59 for 4 months'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),
            FilledButton(
              onPressed: _purchasing ? null : _purchase,
              child: _purchasing
                  ? const CircularProgressIndicator()
                  : Text(active ? 'Renew Plan - Rs 59' : 'Subscribe - Rs 59 / 4 months'),
            ),
          ],
        ),
      ),
    );
  }
}
