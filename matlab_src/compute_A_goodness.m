function [goodness] = compute_A_goodness(A)

    if all(diag(A) < 1)
       goodness = 0; 
    else
        goodness = nnz(diag(A ./ max(max(A)) < 0.01));
    %     goodness = std(diag(A))   % bad results with this on big feature?
        %spaces
    end
end

