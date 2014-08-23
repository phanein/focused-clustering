%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% solving constraint optimization problem using iterative projection
%
% Eric Xing
% UC Berkeley
% Jan 15, 2002
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function [A, converged] = ... 
iter_projection_new2(X, S, D, A, w, t, maxiter)  

% ---------------------------------------------------------------------------
% Input 
% X: data
% S: similarity constraints (in the form of a pairwise-similarity matrix)
% D: disimilarity constraints (in the form of a pairwise-disimilarity matrix)
% A: initial distance metric matrix
% w: a weight vector originated from similar data (see paper)
% upper bound of constraint C1 (the sum of pairwise distance bound)
% maxiter: maximum iterations
%
% Output
% A: the solution of distance metric matrix
% converged: indicator of convergence
% iters: iterations passed until convergence 
% ---------------------------------------------------------------------------

s = size(X)
N = s(1);     % number of examples
d = s(2);     % dimensionality of examples
error1=1e10; error2=1e10;
threshold2 = 0.01;% error-bound of main A-update iteration
epsilon = 0.01;   % error-bound of iterative projection on C1 and C2
maxcount = 100;

w1 = w/norm(w);    % make 'w' a unit vector
t1 = t/norm(w);    % distance from origin to w^T*x=t plane

count=1;
alpha = 0.1;         % initial step size along gradient

grad1 = fS1(X, S, A, N, d);   % gradient of similarity constraint function
grad2 = fD1(X, D, A, N, d);   % gradient of dissimilarity constraint func.
M = grad_projection(grad1, grad2, d); % gradient of fD1 orthognal to fS1


A_last = A;        % initial A
done = 0;

while (~done)

 % projection of constrants C1 and C2 ______________________________
 % _________________________________________________________________
 A_update_cycle=count
 projection_iters = 0;
 satisfy=0;

 while projection_iters < maxiter & ~satisfy

   A0 = A;
   % _____________________________________________________________
   % first constraint:
   % f(A) = \sum_{i,j \in S} d_ij' A d_ij <= t              (1)
   % (1) can be rewritten as a linear constraint: w^T x = t, 
   % where x is the unrolled matrix of A, 
   % w is also an unroled matrix of W where
   % W_{kl}= \sum_{i,j \in S}d_ij^k * d_ij^l
 
   x0= unroll(A0);
   if w' * x0 <= t
     A = A0;
     x = x0;  % BP added this part
   else 
     x = x0 + (t1-w1'*x0)*w1;   
     A = packcolume(x, d, d);
   end
  
   fDC1 = w'*x;    % this is actually just 't'
   A_1 = A;        % resulting A from constraint 1

   % __________________________________________________________________
   % second constraint:
   % PSD constraint A>=0
   % project A onto domain A>0

   A = (A + A')/2;  % enforce A to be symmetric
   [V,L] = eig(A);  % V is an othornomal matrix of A's eigenvectors, 
		   % L is the diagnal matrix of A's eigenvalues, 
   L = max(L, 0);
   A = V*L*V'; 

   fDC2 = w'*unroll(A);
   A_2 = A;       % resulting A from constraint 2
  
   % __________________________________________________________________

   error2 = (fDC2-t)/t;
   projection_iters = projection_iters + 1;

   if error2 > epsilon 
     satisfy=0;
   else
     satisfy=1;   % loop until constrait is not violated after both projections
   end

 end  % end projection on C1 and C2

 projection_iters            
 %[fDC1 fDC2]
 %[error1, error2]


  % __________________________________________________________________
  % third constraint: Gradient ascent
  % max: g(A)>=1
  % here we suppose g(A) = fD(A) = \sum_{I,J \in D} sqrt(d_ij' A d_ij)

  obj_previous = fD(X, D, A_last, N, d)           % g(A_old)
  obj = fD(X, D, A, N, d)                         % g(A): current A  

  if  (obj > obj_previous | count == 1) & (satisfy ==1)

  % if projection of 1 and 2 is successful, 
  % and such projection imprives objective function, 
  % slightly increase learning rate, and updata from the current A
 
    alpha =  alpha * 1.05;  A_last = A; obj
    grad2 = fS1(X, S, A, N, d);
    grad1 = fD1(X, D, A, N, d);
    M = grad_projection(grad1, grad2, d);
    A = A + alpha*M;
 
  else
  % if projection of 1 and 2 failed, 
  % or obj <= obj_previous due to projection of 1 and 2, 
  % shrink learning rate, and re-updata from the previous A 

    alpha = alpha/2; 
    A = A_last + alpha*M;

  end; 

  A % BP
  
  delta = norm(alpha*M, 'fro')/norm(A_last, 'fro')
  count = count + 1;
  if count == maxcount | delta <threshold2,
    done = 1;
  end;
  
end;


if delta > threshold2,
  converged=0;
else
  converged=1;
end;

A = A_last;

