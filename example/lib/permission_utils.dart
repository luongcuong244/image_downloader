import 'dart:io';

import 'package:device_info_plus/device_info_plus.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:image_downloader_example/open_app_setting_dialog.dart';
import 'package:permission_handler/permission_handler.dart';

class PermissionUtils {
  static Future<bool> checkPermissionAndRequestStoragePermission(
      BuildContext context, {Function()? goToSettings}) async {
    final storagePermission = await getStoragePermission();

    if (await storagePermission.request().isGranted) {
      return true;
    }
    var status = await storagePermission.request();
    if (status.isGranted) {
      if (kDebugMode) {
        print('Permission is granted');
      }
      return true;
    } else if (status.isDenied) {
      if (kDebugMode) {
        print('Permission is denied');
      }
      return false;
    } else {
      if (kDebugMode) {
        print('Permission is permanently denied');
      }
      showGeneralDialog(
          context: context,
          transitionDuration: const Duration(milliseconds: 0),
          pageBuilder: (context, animation1, animation2) {
            return OpenAppSettingDialog(goToSetting: () {
              goToSettings?.call();
              Navigator.of(context).pop();
            });
          });
      return false;
    }
  }

  static Future<bool> isStoragePermissionGranted() async {
    final storagePermission = await getStoragePermission();
    return storagePermission.isGranted;
  }

  static Future<Permission> getStoragePermission() async {
    if (Platform.isAndroid) {
      var androidInfo = await DeviceInfoPlugin().androidInfo;
      var sdkInt = androidInfo.version.sdkInt;
      if (sdkInt >= 33) {
        return Permission.photos;
      } else {
        return Permission.storage;
      }
    } else {
      return Permission.photos;
    }
  }
}
