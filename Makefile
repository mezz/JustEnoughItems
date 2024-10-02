switch_java:
	export JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_362`
bd:
	JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_362`
	./gradlew build
	make deploy
deploy:
	rm /Users/mcarver/Documents/curseforge/minecraft/Instances/TestJEI/mods/jei_1.10.2-3.14.8.jar
	cp build/libs/jei_1.10.2-3.14.8.jar /Users/mcarver/Documents/curseforge/minecraft/Instances/TestJEI/mods/
