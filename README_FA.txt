محاسبه‌گر SL / TP

1) Android Studio را نصب کنید.
2) پوشه SLTP_Calculator را با گزینه Open در Android Studio باز کنید.
3) صبر کنید Gradle Sync کامل شود.
4) از منوی Build گزینه Build APK(s) را بزنید.
5) فایل APK در مسیر app/build/outputs/apk/debug/app-debug.apk ساخته می‌شود.

منطق محاسبه:
Long:
 a = fee% * trade volume
 b = profit% * base volume
 c = a + RR*b
 d = b - a
 f = d/trade - 1
 e = c/trade + 1
 SL = f*entry
 TP = e*entry

Short:
 f = d/trade + 1
 e = c/trade - 1
 SL = f*entry
 TP = e*entry

نکته: درصدها به صورت درصد معمولی وارد می‌شوند؛ مثلاً 0.2 یعنی 0.2 درصد و برنامه آن را به 0.002 تبدیل می‌کند.
