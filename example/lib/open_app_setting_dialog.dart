import 'package:flutter/material.dart';
import 'package:app_settings/app_settings.dart';

class OpenAppSettingDialog extends StatelessWidget {

  final Function()? goToSetting;
  const OpenAppSettingDialog({
    super.key,
    this.goToSetting,
  });

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(
        "Permission required",
        style: const TextStyle(
          color: Color(0xFF1A1B1D),
          fontSize: 18,
          fontWeight: FontWeight.w500,
          fontFamily: 'Rubik',
          decoration: TextDecoration.none,
        ),
      ),
      content: Text(
        "Please allow storage permission to download emoji.",
        style: const TextStyle(
          color: Color(0xFF1A1B1D),
          fontSize: 14,
          fontWeight: FontWeight.w400,
          fontFamily: 'Rubik',
          decoration: TextDecoration.none,
        ),
      ),
      actions: [
        TextButton(
          onPressed: () {
            goToSetting?.call();
            AppSettings.openAppSettings();
          },
          child: Text(
            "Go to setting",
            style: const TextStyle(
              color: Color(0xFF007AFF),
              fontSize: 16,
              fontWeight: FontWeight.w500,
              fontFamily: 'Rubik',
              decoration: TextDecoration.none,
            ),
          ),
        ),
      ],
    );
  }
}