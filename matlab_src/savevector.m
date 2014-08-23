function savevector( filename, vector )
%SAVEVECTOR Saves a vector in a format like an edge list   
   
    fid = fopen(filename,'W');    

    if iscell(vector)
        for a = 1:length(vector);
            if ischar(vector{a})
                fprintf( fid,'%d %s\n', a, vector{a} );
            elseif mod(vector{a},1) == 0
                fprintf( fid,'%d %d\n', a, vector{a} );
            else
                fprintf( fid,'%d %f\n', a, vector{a} );
            end
        end
    elseif isvector(vector)
        [i,j,val] = find(vector);
        for a = 1:length(i);
            if mod(val(a),1) == 0
                fprintf( fid,'%d %d\n', i(a), val(a) );
            else
                fprintf( fid,'%d %f\n', i(a), val(a) );
            end
        end                
    else
        assert(0 == 1, 'Unable to save vector!');
    end
    fclose(fid);
end