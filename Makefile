build-release:
	gradle cargoBuild -PcargoProfile=release
	gradle assembleRelease

test-in-ci:
	gradle cargoBuild
	gradle test
