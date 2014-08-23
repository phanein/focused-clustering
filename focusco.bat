@echo off
set CUR_DIR=%CD%

set edge_file=%1
set feature_file=%2
set similar_file=%3
if "%~4"=="" goto no_arg4
set output_file=%4
goto done_parse
:no_arg4
set output_file=focusco.out
:done_parse
set intermediate_file=%output_file%.weighted.edges

echo Focused Cluster and Outliers - Distance Metric Learning
echo %CUR_DIR%\distance_metric_learning(arg1,arg2)
echo edge file=%edge_file%
echo feature file=%feature_file%
echo node similarity file=%similar_file%
echo distance metric file=%intermediate_file%
echo output file=%output_file%
start matlab -nosplash -nodesktop -minimize -r focusco_main('%edge_file%','%feature_file%','%similar_file%','file_output','%intermediate_file%') -logfile focusco.log