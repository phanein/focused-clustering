% Bryan Perozzi

function [A] = testPGDM()

    datasize = 300;

    [X,S,D] = test1(datasize, 50, 150);
%     [X,S,D] = test2(datasize, 50, 150);
    %[X,S,D] = test3(datasize, 10);
%     A = PGDM(X,S,D);

    A = Newton(X, S, D, 1)
    
    figure(2);
    colormap();
    imagesc(A);
    colorbar();
    
    figure(3);
    B = zeros(size(X));
    for i=1:datasize
        B(i,:) = A^(1/2)*X(i,:)';
    end
    scatter3(B(:,1),B(:,2),B(:,3));
end

% test 1: uniform data, similar data from narrow guassian
function [X,S,D] = test1(num_examples, num_dimensions, num_similar)
    % number similar examples
    num_S = num_similar;

    % generate data
    X = rand(num_examples, num_dimensions);
    
    % make some stuff similar
    X(1:num_S) = normal(1, num_S, 1, 0.1);
    
    % make pairwise similarity matrix
    %s = eye(num_examples);
    s = zeros(num_examples);
    s(1:num_S,1:num_S) = ones(num_S);

    % make pairwise dissimilarity matrix
    d = zeros(num_examples);
    start_dissim = num_examples - num_S + 1
    end_dissim = num_examples
    d(start_dissim : end_dissim, start_dissim:end_dissim ) =  ...
        (ones(num_S) - eye(num_S)); % indexing by 1 is annoying 
    
    S = s;
    % weird weight vector
    D = d;
end

% test 2: guassian data, similar data has a couple dimensions with narrower
% guassians
function [X,S,D] = test2(num_examples, num_dimensions, num_similar)
    % number similar examples
    num_S = num_similar;

    % generate data
    X = zeros(num_examples, num_dimensions);
    for j = 1:num_dimensions
        mean = 1000*rand(1);
        std = 100*rand(1);
        X(:,j) = normal(num_examples, 1, mean, std);
    end
    
    % make some stuff similar
    for j = 1:5
        mean = 1000*rand(1);
        std = 10*rand(1);
        X(1:num_S,j) = normal(num_S, 1, mean, std)
    end
        
    % make pairwise similarity matrix
    s = zeros(num_examples);
    s(1:num_S,1:num_S) = ones(num_S);

    % make pairwise dissimilarity matrix
    d = zeros(num_examples);
    start_dissim = num_examples - num_S + 1
    end_dissim = num_examples
    d(start_dissim : end_dissim, start_dissim:end_dissim ) =  ...
        (ones(num_S) - eye(num_S)); % indexing by 1 is annoying 
    
    S = s;
    % weird weight vector
    D = d;
end

% test 3: two seperate 2D guassians, just like in the paper
function [X,S,D] = test3(num_examples, num_similar)
    % number similar examples
    num_S = num_similar;
    num_dimensions = 3;

    % generate data
    X = zeros(num_examples, num_dimensions);
    for j = 1:num_dimensions
        mean = 10*rand(1) - 5;
        std = 1;
        
        if j == 3
            std = 1;
        end
        
        X(:,j) = normal(num_examples, 1, mean, std);
    end
    
    % make some stuff similar
    for j = 1:num_dimensions
        mean = 10*rand(1) + 5;
        std = 1;
        
        if j == 3
            std = 1;
        end        
        
        X(1:(num_examples/2 - 1),j) = normal((num_examples/2 - 1), 1, mean, std);
    end

    %X(:,3) = 0.1.*ones(num_examples,1);
    
    figure(1);
    scatter3(X(:,1),X(:,2),X(:,3))
    
    % make pairwise similarity matrix
    s = zeros(num_examples);
    s(1:num_S,1:num_S) = ones(num_S);

    % make pairwise dissimilarity matrix
    d = zeros(num_examples);
    start_dissim = num_examples - num_S + 1
    end_dissim = num_examples
    d(start_dissim : end_dissim, start_dissim:end_dissim ) =  ...
        (ones(num_S) - eye(num_S)); % indexing by 1 is annoying 
    
    S = s;
    % weird weight vector
    D = d;
end

function [X,S,D] = test4(num_examples, num_dimensions, num_similar)

end

function A = PGDM(X,S,D)
    num_dimensions = size(X,2);
    W = makeWfast(X, S);
    w = unroll(W);

    % BP: how to initialize A?
    % eye() was a bad initialization, ended prematurely, frequently didn't
    % learn anything useful (sometimes did though)
    % rand() somehow got complex numbers in the output.  results not as
    % expected
    % zeros() works best so far.
    original_A = zeros(num_dimensions);
    A = iter_projection_new2(X, S, D, original_A, w, 1, 1000);
end

function [random] = normal(n, m, mu, sigma)
 random = mu + sigma.*randn(n,m);
end

function [W] = makeW(X, S)
    % W is a weight matrix made from S
    % W_{kl}= \sum_{i,j \in S}d_ij^k * d_ij^l
    sizeX = size(X);
    W = zeros(sizeX(2));  % W is matrix for weighting features |f|x|f|
    for k = 1:sizeX(2)
        for l = 1:sizeX(2)
            for i = 1:sizeX(1)
                for j = 1:sizeX(1)
                    if S(i,j) == 1
                        d_ij = X(i,:) - X(j,:);               % difference between 'i' and 'j'
                        W(k,l) = W(k,l) + d_ij(k) *d_ij(l);
                    end
                end
            end   
        end
    end
    W
end

function W = makeWfast(X,S)
    sizeX = size(X);
    W = zeros(sizeX(2));  % W is matrix for weighting features |f|x|f|
    
    % for all similar elements (S_ij is non-zero)
    [i_arr,j_arr] = find(S);
    for a = 1:numel(i_arr)
        for b = 1:numel(j_arr)        
            i = i_arr(a);
            j = j_arr(b);
    
            % calculate their difference
            d_ij = X(i,:) - X(j,:);
            
            % add their weights
            %W(k,l) = W(k,l) + d_ij(k) *d_ij(l);
            W = W + d_ij'*d_ij;
        end
    end
    W
end

function testW()
    X = [1,2,3;2,4,6]
    S = [0,1;1,0]
    
    makeW(X,S)
    makeWfast(X,S)
end