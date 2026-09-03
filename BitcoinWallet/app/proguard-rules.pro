# bitcoinj uses reflection in a few places; keep classes to avoid runtime issues
-keep class org.bitcoinj.** { *; }
-dontwarn org.bitcoinj.**
