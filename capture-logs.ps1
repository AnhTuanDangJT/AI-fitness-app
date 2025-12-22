# PowerShell script to capture Spring Boot logs
# Run this in the same directory where you start Spring Boot

# If running with mvn spring-boot:run, redirect output:
# mvn spring-boot:run *> spring-boot-output.log

# If running with java -jar, redirect output:
# java -jar target/aifitness-backend-1.0.0.jar *> spring-boot-output.log

Write-Host "To capture Spring Boot logs, restart your backend with output redirection:"
Write-Host ""
Write-Host "For Maven:"
Write-Host "  mvn spring-boot:run *> spring-boot-output.log"
Write-Host ""
Write-Host "For JAR:"
Write-Host "  java -jar target/aifitness-backend-1.0.0.jar *> spring-boot-output.log"
Write-Host ""
Write-Host "Then try signup again and check spring-boot-output.log for errors"














