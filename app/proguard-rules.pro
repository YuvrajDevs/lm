# App code needs no extra rules: no reflection, and JSON is read with org.json.
# Google Play Billing ships its own consumer rules.

# Glance creates widget action callbacks by class name.
-keep class * implements androidx.glance.appwidget.action.ActionCallback { <init>(); }
