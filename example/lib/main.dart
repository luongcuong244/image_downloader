import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_downloader/image_downloader.dart';
import 'package:image_downloader_example/permission_utils.dart';
import 'package:fluttertoast/fluttertoast.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: ElevatedButton(
            onPressed: () {
              _downloadEmoji(context, "https://www.gstatic.com/android/keyboard/emojikitchen/20210521/u1fa84/u1fa84_u1f600.png",);
            },
            child: Text("Download emoji"),
          ),
        ),
      ),
    );
  }

  void _downloadEmoji(BuildContext context, String emojiUrl) async {
    var isPermissionGranted =
    await PermissionUtils.checkPermissionAndRequestStoragePermission(
        context);
    if (!isPermissionGranted) {
      return;
    }

    bool isDownloaded = false;
    await ImageDownloader.downloadImage(
      emojiUrl,
    ).then((value) {
      isDownloaded = true;
    }).catchError((error) {
      print("error: ${error.toString()}");
      isDownloaded = false;
    });

    if (!isDownloaded) {
      Fluttertoast.showToast(
        msg: "Failed to download",
        toastLength: Toast.LENGTH_SHORT,
        gravity: ToastGravity.BOTTOM,
      );
      return;
    }
    Fluttertoast.showToast(
      msg: "Downloaded successfully",
      toastLength: Toast.LENGTH_SHORT,
      gravity: ToastGravity.BOTTOM,
    );
  }
}
