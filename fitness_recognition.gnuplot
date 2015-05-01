set term "jpeg"
set output filename . ".jpg"

set title filename noenhanced
set xlabel "Time (s)"
set ylabel "Magnitude (m/s^2)"
plot filename using ($1/1000000000):2 with lines    title "Acceleration", \
     filename using ($1/1000000000):3 with impulses title "Repetition"
