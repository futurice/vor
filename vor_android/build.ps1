param (
    [switch]$lint = $false,
    [switch]$test = $false,
    [switch]$connectedTest = $false
)

if ($connectedTest) {
	./gradlew clean build connectedCheck -PdisablePreDex --stacktrace
} else {
	./gradlew clean build -PdisablePreDex
}

if ($lint) {
	ii ./mobile/build/outputs/lint-results.html
	ii .\scampi_client\build\outputs\lint-results.html
}

if ($connectedTest) {
	ii ./mobile/build/reports/androidTests/connected/index.html
}

if ($test) {
	./gradlew test --stacktrace 					
	ii .\mobile\build\reports\tests\standarduserDebug\index.html
	ii .\mobile\build\reports\tests\standarduserRelease\index.html
	ii .\mobile\build\reports\tests\superuserDebug\index.html
	ii .\mobile\build\reports\tests\superuserRelease\index.html
}

