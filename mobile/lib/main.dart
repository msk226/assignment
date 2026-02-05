import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const PointRouletteApp());
}

class PointRouletteApp extends StatelessWidget {
  const PointRouletteApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Point Roulette',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: const ColorScheme.light(
          primary: AppColors.primary,
          onPrimary: AppColors.primaryFg,
          surface: AppColors.surface,
          onSurface: AppColors.neutral,
        ),
        scaffoldBackgroundColor: AppColors.background,
        useMaterial3: true,
      ),
      home: const WebViewScreen(),
    );
  }
}

class AppColors {
  static const primary = Color(0xFFD9FD01);
  static const primaryFg = Color(0xFF1A1A1A);
  static const background = Color(0xFFFFFFFF);
  static const surface = Color(0xFFF9FAFB);
  static const neutral = Color(0xFF111827);
}

class WebViewScreen extends StatefulWidget {
  const WebViewScreen({super.key});

  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen> {
  static const String initialUrl =
      'https://assignment-nine-lemon.vercel.app/home';

  late final WebViewController _controller;
  bool _isLoading = true;
  bool _hasError = false;
  String? _errorDescription;

  @override
  void initState() {
    super.initState();
    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setBackgroundColor(AppColors.background)
      ..setOnJavaScriptAlertDialog((request) async {
        await _showAlert(request.message);
      })
      ..setOnJavaScriptConfirmDialog((request) async {
        return _showConfirm(request.message);
      })
      ..setOnJavaScriptTextInputDialog((request) async {
        return _showPrompt(request.message, request.defaultText);
      })
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (_) {
            setState(() {
              _isLoading = true;
              _hasError = false;
              _errorDescription = null;
            });
          },
          onPageFinished: (_) {
            setState(() {
              _isLoading = false;
            });
          },
          onWebResourceError: (error) {
            setState(() {
              _hasError = true;
              _isLoading = false;
              _errorDescription = error.description;
            });
          },
        ),
      )
      ..loadRequest(Uri.parse(initialUrl));
  }

  Future<void> _handleBack() async {
    if (await _controller.canGoBack()) {
      await _controller.goBack();
      return;
    }
    SystemNavigator.pop();
  }

  Future<void> _showAlert(String message) async {
    if (!mounted) return;
    await showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: const Text('알림'),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('확인'),
            ),
          ],
        );
      },
    );
  }

  Future<bool> _showConfirm(String message) async {
    if (!mounted) return false;
    final result = await showDialog<bool>(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: const Text('확인'),
          content: Text(message),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text('취소'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              child: const Text('확인'),
            ),
          ],
        );
      },
    );
    return result ?? false;
  }

  Future<String> _showPrompt(String message, String? defaultText) async {
    if (!mounted) return '';
    final controller = TextEditingController(text: defaultText ?? '');
    final result = await showDialog<String>(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: const Text('입력'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(message),
              const SizedBox(height: 12),
              TextField(
                controller: controller,
                autofocus: true,
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(''),
              child: const Text('취소'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(controller.text),
              child: const Text('확인'),
            ),
          ],
        );
      },
    );
    return result ?? '';
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: false,
      onPopInvoked: (_) {
        _handleBack();
      },
      child: Scaffold(
        body: SafeArea(
          child: Stack(
            children: [
              if (_hasError)
                ErrorView(
                  description: _errorDescription,
                  onRetry: () {
                    setState(() {
                      _hasError = false;
                      _isLoading = true;
                    });
                    _controller.loadRequest(Uri.parse(initialUrl));
                  },
                )
              else
                WebViewWidget(controller: _controller),
              if (_isLoading) const LoadingOverlay(),
            ],
          ),
        ),
      ),
    );
  }
}

class LoadingOverlay extends StatelessWidget {
  const LoadingOverlay({super.key});

  @override
  Widget build(BuildContext context) {
    return Positioned.fill(
      child: Container(
        color: AppColors.surface.withOpacity(0.8),
        child: const Center(
          child: SizedBox(
            width: 36,
            height: 36,
            child: CircularProgressIndicator(
              strokeWidth: 3,
              color: AppColors.neutral,
            ),
          ),
        ),
      ),
    );
  }
}

class ErrorView extends StatelessWidget {
  const ErrorView({
    super.key,
    required this.onRetry,
    this.description,
  });

  final VoidCallback onRetry;
  final String? description;

  @override
  Widget build(BuildContext context) {
    return Container(
      color: AppColors.background,
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(999),
              ),
              child: const Text(
                'Point Roulette',
                style: TextStyle(
                  color: AppColors.primaryFg,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            const SizedBox(height: 16),
            const Text(
              '네트워크에 연결할 수 없어요',
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
                color: AppColors.neutral,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 8),
            Text(
              description?.isNotEmpty == true
                  ? description!
                  : '잠시 후 다시 시도해주세요.',
              style: const TextStyle(
                color: AppColors.neutral,
                fontWeight: FontWeight.w500,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: onRetry,
                style: FilledButton.styleFrom(
                  backgroundColor: AppColors.primary,
                  foregroundColor: AppColors.primaryFg,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  textStyle: const TextStyle(
                    fontWeight: FontWeight.w700,
                  ),
                ),
                child: const Text('재시도'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
