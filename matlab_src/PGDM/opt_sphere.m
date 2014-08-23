function A = opt_sphere(X, S, D, maxiter)

% Preprocessing: First sphere the data
sphereMult = cov(X)^-0.5;
sphereX = X*sphereMult;

s = size(sphereX);
N = s(1);     % number of examples
d = s(2);     % dimensionality of examples            

A = eye(d,d)*0.1;
W = zeros(d,d);

for i = 1:N,
  for j = i+1:N,
    if S(i,j) == 1,
      d_ij = sphereX(i,:) - sphereX(j,:);
      W = W + d_ij'*d_ij;
    end;
  end;
end;     

w = unroll(W);
t = w' * unroll(A)/100;

[A, converge] = iter_projection_new2(sphereX, S, D, A, w, t, maxiter);
  
% Now unsphere the distance metric so that we get a version
% for the original, unsphered data.

unspheredA = inv(sphereMult)*A*inv(sphereMult);

A = unspheredA;



