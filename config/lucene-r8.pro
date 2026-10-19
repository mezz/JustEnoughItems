# When jdk.incubator.vector is enabled, Lucene loads this provider by name and
# throws LinkageError if it is missing. Keep it so shrinking cannot break that runtime.
-keep class mezz.jei.modshade.org.apache.lucene.internal.vectorization.PanamaVectorizationProvider { *; }

# Lucene finds attribute constructors at runtime, so R8 can mistake them for unused code.
# Keep the no-argument constructors so Lucene can still create attributes after shrinking.
-keep class * extends mezz.jei.modshade.org.apache.lucene.util.AttributeImpl {
    public <init>();
}
