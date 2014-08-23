%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% solving constraint optimization problem using Newton-Raphson method
%
% Eric Xing
% UC Berkeley
% Jan 15, 2002
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Bryan Perozzi - modified for sparsity, etc. See BP comments.

function A = Newton_sparse_top_k(data, S, D, C, topk)

size_data=size(data);
orig_N=size_data(1);
orig_d=size_data(2);

X=data;

fudge = 0.000001;
threshold1 = 0.001;
reduction = 2;

% suppose d is a column vector
% sum(d'Ad) = sum(trace(d'Ad)) = sum(trace(dd'A)) 
%           = trace(sum(dd'A) = trace(sum(dd')A)

s_total_sum = zeros(1,orig_d);
% BP another speed optimization
[i_arr, j_arr, s_arr] = find(S);
for cnt = 1:numel(i_arr)
    i = i_arr(cnt);
    j = j_arr(cnt);
    %d_ij = X(i,:) - X(j,:);    
    if S(i,j) ~= 0
      s_total_sum = s_total_sum + X(i,:) + X(j,:);
    end
end

s_total_sum;

% BP only keep the top k dimensions that are important in s
[top_sim, top_idx] = sort(s_total_sum(1,:), 'descend');
top_similarity_indices = top_idx(1,(1:topk));
top_similarity_counts = top_sim(1,(1:topk));
sorted_top_idx = sort(top_idx(1,(1:topk)));
X = X(:,sorted_top_idx);

size_new_data = size(X);
N=size_new_data(1);
d=size_new_data(2);

a=ones(d,1);
s_sum = zeros(1,d);
d_sum = zeros(1,d);

% BP another speed optimization
[i_arr, j_arr, s_arr] = find(D);
for cnt = 1:numel(i_arr)
    i = i_arr(cnt);
    j = j_arr(cnt);
    d_ij = X(i,:) - X(j,:) + ~(X(i,:) | X(j,:));
    if D(i,j) == 1
      d_sum = d_sum + d_ij.^2;
    end
end
[i_arr, j_arr, s_arr] = find(S);
for cnt = 1:numel(i_arr)
    i = i_arr(cnt);
    j = j_arr(cnt);
    d_ij = X(i,:) - X(j,:) + ~(X(i,:) | X(j,:));
    if S(i,j) == 1
      s_sum = s_sum + d_ij.^2;
    end
end

s_sum;
d_sum;

tt=1;
error=1;
% BP added outer loop constraint, it got stuck in an infinite loop once?
while error > threshold1 && tt < 50
  fprintf('Iteration: %d\n', tt)
  [fD0, fD_1st_d, fD_2nd_d] = D_constraint_sparse(X, D, a, N, d);
  obj_initial =  s_sum*a + C*fD0; 
  fS_1st_d = s_sum;                    % first derivative of the S constraints

  Gradient = fS_1st_d - C*fD_1st_d;            % gradient of the objective
  Hessian = - C*fD_2nd_d + fudge*eye(d);      % Hessian of the objective
%     invHessian = inv(Hessian);
%     step = invHessian*Gradient';
%   
  % BP - Replacing inverse (using A\b instead of inv(A)*b)
  step = Hessian\Gradient';
  
  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
  % Newton-Raphson update
  % search over optimal lambda
  
  lambda=1;        % initial step-size
  t=1;             % counter
  atemp = a - lambda*step;
  atemp = max(atemp, 0);

  obj = s_sum*atemp + C*D_objective_sparse(X, D, atemp, N, d);
  fprintf('Objective: %f\n', obj)
  
  % BP - this doesn't always work?
  obj_previous = obj * 1.1;    %  just to get the while loop started  

  % BP without initialization this can bug out
  a_previous = atemp;
  
  while obj < obj_previous
    lambda_previous = lambda;
    obj_previous = obj;
    a_previous = atemp;
    lambda = lambda/reduction; 
    atemp = a - lambda*step;
    atemp = max(atemp, 0);
    obj = s_sum*atemp + C*D_objective_sparse(X, D, atemp, N, d);
    t=t+1;   % inner counter
  end    % line search for lambda that minimize obj
  
  a = a_previous;
  
  error = abs((obj_previous - obj_initial)/obj_previous);
  tt = tt + 1;   % outer counter
end
a(a~=0);


% reconstruct diagonal for original feature space
full_solution = zeros(1,orig_d);
full_solution(sorted_top_idx) = a;
A=spdiags(full_solution', 0, orig_d, orig_d);
