function [ DM, S, D ] = distance_metric_learning_manual(X, s, n_dimsim_pairs, num_vertices, C, topk, dml_version)
%DISTANCE_METRIC_LEARNING Samples S from similar pairs in the similarity
%   region, and D randomly from points outside the S region
% s the [|S| x 2] similar rows to make into pairs
% n_dimsim_pairs number of dissimilar entries to generate
% num_vertices the # vertices in G
% exclusion_start the similar community to use
% exclusion_end the similar community to use

    addpath('PGDM')

    i_arr = zeros(2*size(s,1),1);
    j_arr = zeros(2*size(s,1),1);
    s_arr = ones(2*size(s,1),1);
    
    for z=1:size(s,1)
        pair = s(z,:);
        S(pair(1),pair(2)) = 1;
        S(pair(2),pair(1)) = 1;
        y = 2*z;
        i_arr(y - 1) = pair(1);
        j_arr(y - 1) = pair(2);
        i_arr(y) = pair(2);
        j_arr(y) = pair(1);
    end
   
    S = sparse(i_arr, j_arr, s_arr, num_vertices, num_vertices);

    NNZ_S = nnz(S);
    
    % sample randomly to create dissimilar pairs, D
    i_arr = zeros(n_dimsim_pairs,1);
    j_arr = zeros(n_dimsim_pairs,1);
    s_arr = ones(n_dimsim_pairs,1);        
    for z=1:n_dimsim_pairs       
        d1 = randi([1, num_vertices]);
        while any(d1 == s)
            d1 = randi([1, num_vertices]);
        end
        d2 = randi([1, num_vertices]);

        while any(any(d2 == s)) || d1 == d2
            d2 = randi([1, num_vertices]);
        end
        
        i_arr(z) = d1;
        j_arr(z) = d2;
    end
    
    D = sparse(i_arr, j_arr, s_arr, num_vertices, num_vertices);

    if strcmp(dml_version, 'sparse')
        DM = Newton_sparse_top_k(X, S, D, C, topk);
    else
        DM = Newton(X, S, D, C);
    end
end

