# Fitness Recognition

## Recording and Plotting

1. Build and install app on watch.
   
   ````
   ./gradlew clean app:installDebug
   ````

2. Record a trace, noting the trace number.
3. Pull the trace from the watch.
   
   ````
   adb -e pull /sdcard/Documents/fitness_recognition/trace_8
   ````

4. Process the trace to find repetitions.
   
   ````
   ./gradlew clean processor:fatJar
   java -Done-jar.silent=true -jar processor/build/libs/processor-standalone.jar < trace_8 > trace_8.out
   ````

5. Plot trace to see where the repetitions were detected.

   ````
   gnuplot -e "filename='trace_8.out'" fitness_recognition.gnuplot
   open trace_8.out.jpg
   ````

