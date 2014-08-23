% Bryan Perozzi

function [Weighted] = reweigh(G, X, A)
%REWEIGH Reweigh graph G given features X and distance metric A
    A_half = A.^(1/2);  % sparse, so can do this
    
    nnz(G)
    [g_arr,h_arr] = find(G);
    weights = zeros(numel(g_arr),1);    
    X = X';
    
    for  a = 1:numel(g_arr)        
        g = g_arr(a);
        h = h_arr(a);
        % calculate their difference
        d_ij = X(:,g) - X(:,h);
        % weigh and convert distance vector d to similarity vector 1/d
        weight = 1.0/(1.0 + norm(A_half*d_ij, 2));
        weights(a) = weight;        
    end       
    
    Weighted = sparse(g_arr, h_arr, weights, size(G,1), size(G,2));
end
