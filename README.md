# kotlin-toy-compiler

A toy Kotlin compiler.
Only Compiled main functtion. only 
Call `kotlin.io.println`

## How to run
```kotlin
//main.kt
fun main() {
    println("hello world.")
    println("this code run .Net")
}
```
```bash
 ./gradlew run --args="./main.kt -o main" 
```
