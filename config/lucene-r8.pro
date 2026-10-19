# Lucene finds attribute constructors at runtime, so R8 can mistake them for unused code.
# Keep the no-argument constructors so Lucene can still create attributes after shrinking.
-keep class * extends mezz.jei.modshade.org.apache.lucene.util.AttributeImpl {
    public <init>();
}
