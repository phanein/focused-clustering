function A = opt(X, S, D, maxiter)

% each row is a example:

s = size(X);
N = s(1);     % number of examples
d = s(2);     % dimensionality of examples            

A = eye(d,d)*0.1;
W = zeros(d,d);

for i = 1:N,
  for j = i+1:N,
    if S(i,j) == 1,
      d_ij = X(i,:) - X(j,:);
      W = W + (d_ij'*d_ij);
    end;
  end;
end;     

w = unroll(W);
t = w' * unroll(A)/100;

[A, converge] = iter_projection_new2(X, S, D, A, w, t, maxiter);
  









