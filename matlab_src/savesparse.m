function savesparse( filename, sparse_matrix )
%SAVESPARSE Save a sparse matrix as an edge list
    [i,j,val] = find(sparse_matrix);
    
    fid = fopen(filename,'W');
    for a = 1:numel(i)
        fprintf( fid,'%d %d %f\n', i(a),j(a),val(a) );
    end
    fclose(fid);
end

