# Spatial Radar 3D v2

Pixel 7a向けのARCore Raw Depthリアルタイム3D再構築アプリです。

## v1との違い

v1は毎フレームの点を2D画面に描くだけでした。v2はRaw Depthとconfidenceをワールド座標に変換し、12cmボクセルへ複数フレームを統合します。安定観測されたボクセルの露出面から三角形メッシュを生成し、カメラ重畳表示と回転可能な3Dマップ表示を提供します。

## 機能

- ARCore Raw Depth + confidence filtering
- 時間方向の深度蓄積
- 12cm voxel surface reconstruction
- 床・壁平面による補強
- 3Dカメラ重畳表示
- ドラッグ・ピンチ対応3Dマップ
- ML Kit端末内人物姿勢検出
- カメラ以外の実行時権限なし
- 録画、顔認証、ネット送信なし

## ビルド

Android Studio Ladybug以降、JDK 17、Android SDK 35を使用します。

```bash
./gradlew :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## 注意

Pixel 7aにはLiDARがないため、透明物、鏡、無地の壁、暗所、動く物体は欠けやすくなります。このアプリの距離や形状を衝突回避など安全用途に使わないでください。
