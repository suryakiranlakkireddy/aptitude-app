class SubscriptionInfo {
  final bool active;
  final DateTime? purchasedAt;
  final DateTime? expiresAt;
  final double amountPaid;

  SubscriptionInfo({
    required this.active,
    this.purchasedAt,
    this.expiresAt,
    required this.amountPaid,
  });

  factory SubscriptionInfo.fromJson(Map<String, dynamic> json) => SubscriptionInfo(
        active: json['active'] ?? false,
        purchasedAt: json['purchasedAt'] != null ? DateTime.parse(json['purchasedAt']) : null,
        expiresAt: json['expiresAt'] != null ? DateTime.parse(json['expiresAt']) : null,
        amountPaid: (json['amountPaid'] as num?)?.toDouble() ?? 0,
      );
}
