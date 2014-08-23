function fd = fD(X, D, A, N, d)

% ---------------------------------------------------------------------------
% the value of dissimilarity constraint function
% f = f(\sum_{ij \in D} distance(x_i, x_j)) 
% i.e. distance can be L1:  \sqrt{(x_i-x_j)A(x_i-x_j)'}) ...
%      f(x) = x ...
% ---------------------------------------------------------------------------

fd = 0.000001;

for i = 1:N
  for j= i+1:N
    if D(i,j) == 1,
      d_ij = X(i,:) - X(j,:);
      distij = distance1(A, d_ij);      % distance between 'i' and 'j'
      fd = fd + distij;        % constraint defined on disimilar set
    end   
  end
end

fd = gF2(fd);

% ___________L1 norm______________
function kd = distance1(A, d_ij)
kd = (d_ij * A * d_ij')^(1/2);

% ___________sqrt(L1 norm)___________
function kd = distance2(A, d_ij)
kd = (d_ij * A * d_ij')^(1/4);

% ___________1-exp(-beta*L1)_________
function kd = distance3(A, d_ij)
beta = 0.5;
kd = 1 - exp(-beta*(sqrt(d_ij * A * d_ij')));

% ___________cover function 1_________
function x = gF1(x1)
x = x1;  
% ___________cover function 1_________
function x = gF2(x1)
x = log(x1);  



