build-release:
	./gradlew cargoBuild -PcargoProfile=release
	./gradlew assembleRelease

build-release-in-ci:
	gradle cargoBuild -PcargoProfile=release
	gradle assembleRelease

test-in-ci:
	gradle cargoBuild
	gradle test
