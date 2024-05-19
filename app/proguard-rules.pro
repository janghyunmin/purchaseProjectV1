# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.


# ====================== [proguard 중요 사항] ========================
# 1. 난독화 적용 시 외부 라이브러리 및 jar 파일 사용 시 패키지 명을 확인해서 난독화 제외 설정 필요 (특정 객체 초기화 부분 클래스에서 패키지명 파악)
# 2. 프로젝트 설정 부분에서 release 모드와 debug 모드 나눠서 설정 가능
# ==================================================================

# ====================== [proguard option] =========================
#-dontwarn 패키지명.** : 지정해서 경고 무시
#-keep class 패키지명.** : 난독화가 필요하지 않은 경우
#-keep interface 패키지명.** : 난독화가 필요하지 않은 경우
#-ignorewarnings : 경고 무시
#-dontoptimize : 최적화 하지 않기
#-dontshrink : 사용하지 않는 메소드 유지
#-keepclassmembers : 특정 클래스 멤버 원상태 유지
#-keepattributes : 내부 클래스 원상태 유지 적용
#-dontshrink : 사용하지 않는 메소드 유지 설정
#-dontoptimize : 최적화하지 않음
#-dontobfuscate : 난독화를 수행 하지 않도록 함
#-verbose : 로그봄 설정
#-ignorewarnings : 경고 무시
#-dontpreverify : 사전 검증기능을 사용하지 않음
#-dontusemixedcaseclassnames : 대소문자가 혼합된 클래스명을 허용하지 않음
# ==================================================================


# ====================== [project settings] ========================
# [디버깅 모드에서 난독화 제외 설정]
#-dontshrink
#-dontoptimize
#-dontobfuscate
# ==================================================================


# -dontwarn 패키지명.** : 지정해서 경고 무시
# -keep class 패키지명.** : 난독화가 필요하지 않은 경우
# -ignorewarnings : 경고 무시
# -dontoptimize : 최적화 하지 않기
# -dontshrink : 사용하지 않는 메소드 유지
# -keepclassmembers : 특정 클래스 멤버 원상태 유지
# -keepattributes : 내부 클래스 원상태 유지 적용

# ========================== [Firebase] ============================
-keepattributes *Annotation*
-keepattributes Signature
# ==================================================================

# ====================== [class and google] ========================
-keep class com.google.android.** { *; }
-keep class google.** { *; }
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class com.android.** { *; }
-keep class com.google.** { *; }
-keep class lambda* { *; }
-keepclassmembers enum * {
    values(...);
    valueOf(...);
}
-keepclasseswithmembers class * {
    native <methods>;
}

-keep public class * extends android.app.Activity { *; }
-keep public class * extends androidx.activity.** { *; }
-keep public class * extends androidx.appcompat.view.** { *; }
-keep public class * extends android.app.Application { *; }
-keep public class * extends androidx.appcompat.** {*; }
-keep public class * extends android.app.Service { *; }
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.content.ContentProvider { *; }
-keep public class * extends android.app.backup.BackupAgentHelper { *; }
-keep public class * extends android.preference.Preference { *; }
-keep public class * extends android.widget.TextView { *; }
-keep public class * extends android.widget.Button { *; }
-keep public class * extends androidx.fragment.app.** { *; }
# ==================================================================


# ==================== [ok http and retrofit] ======================
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
# ==================================================================


# ===================== [webview javascript] =======================
-keepclassmembers class * {
	@android.webkit.JavascriptInterface <methods>;
}
# ==================================================================


# ========================= [tedpermission] =========================
-dontwarn com.gun0912.tedpermission.**
-keep class com.gun0912.tedpermission.** { *; }
-keep interface com.gun0912.tedpermission.** { *; }
# ==================================================================


# ========================== [glide image] =========================
-dontwarn com.bumptech.glide.**
-keepclassmembers class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# ==================================================================

-keep class **.R$*
-keep class run.piece.dev.base.** {*;}
-keep class run.piece.dev.App.** {*;}
-keep class run.piece.dev.R$raw { *; }
-keep public class run.piece.domain.refactoring.deposit.model.** {*;}
-keep public class run.piece.dev.base.* {*;}
-keep public class run.piece.dev.data.* {*;}
-keep public class run.piece.dev.di.* {*;}
-keep public class run.piece.dev.firebase.* {*;}
-keep public class run.piece.dev.model.* {*;}
-keep public class run.piece.dev.retrofit.* {*;}
-keep public class run.piece.dev.view.* {*;}
-keep public class run.piece.dev.widget.* {*;}
-keepclassmembers class **.R$layout { public static <fields>;}
-keepclassmembers class run.piece.dev.view.** {*;}
-keepclassmembers class run.piece.dev.data.** {*;}
-keepclassmembers class run.piece.dev.data.dto.** {*;}
-keepclassmembers class run.piece.dev.widget.** {*;}
-keepclassmembers class run.piece.dev.model.** {*;}
-keepclassmembers class run.piece.dev.retrofit.** {*;}

# DataStore 패키지에 대한 보존 규칙
-keep class androidx.datastore.** { *; }

# DataStore 패키지를 제외한 다른 패키지들에 대한 축소화와 난독화 적용
-keep class !androidx.datastore.** {
    <methods>;          # 클래스 내의 메서드 보존
    <fields>;           # 클래스 내의 필드 보존
}

# New Pdf Proguard Setting
-keep class com.shockwave.**

# 웹소켓 난독화 추가
-keep class run.piece.dev.api.** {*;}

