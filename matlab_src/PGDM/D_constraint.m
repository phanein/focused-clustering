function [fD, fD_1st_d, fD_2nd_d] = D_constraint(X, D, a, N, d)
% Compute the value, 1st derivative, second derivative (Hessian) of 
% a dissimilarity constrant function gF(sum_ij distance(d_ij A d_ij))
% where A is a diagnal matrix (in the form of a column vector 'a'). 

sum_dist = 0;
sum_deri1 = zeros(1,d); 
sum_deri2 = zeros(d,d);

%BP changed this to sparse indexing
[i_arr, j_arr, s_arr] = find(D);
for cnt = 1:numel(i_arr)
    i = i_arr(cnt);
    j = j_arr(cnt);
    if D(i,j) == 1      
       d_ij = X(i,:) - X(j,:);               % difference between 'i' and 'j'
       [dist_ij, deri1_d_ij, deri2_d_ij] = distance1(a, d_ij);
       sum_dist = sum_dist +  dist_ij;
       sum_deri1 = sum_deri1 + deri1_d_ij;
       sum_deri2 = sum_deri2 + deri2_d_ij;
    end
end

    
% for i = 1:N
%   for j= i+1:N
%     if D(i,j) == 1      
%        d_ij = X(i,:) - X(j,:);               % difference between 'i' and 'j'
%        [dist_ij, deri1_d_ij, deri2_d_ij] = distance1(a, d_ij);
%        sum_dist = sum_dist +  dist_ij;
%        sum_deri1 = sum_deri1 + deri1_d_ij;
%        sum_deri2 = sum_deri2 + deri2_d_ij;
%     end
%   end
% end

[fD, fD_1st_d, fD_2nd_d] = gF2(sum_dist, sum_deri1, sum_deri2);


% __________cover function 1_________
function [fD, fD_1st_d, fD_2nd_d] = gF1(sum_dist, sum_deri1, sum_deri2)
% gF1(y) = y
    fD = sum_dist;
    fD_1st_d = sum_deri1;
    fD_2nd_d = sum_deri2;

function [fD, fD_1st_d, fD_2nd_d] = gF2(sum_dist, sum_deri1, sum_deri2)
% gF1(y) = log(y)
    fD = log(sum_dist);
    fD_1st_d = sum_deri1/sum_dist;
    fD_2nd_d = sum_deri2/sum_dist - sum_deri1'*sum_deri1/(sum_dist^2);



function [dist_ij, deri1_d_ij, deri2_d_ij] = distance1(a, d_ij)
% distance and derivatives of distance using distance1: distance(d) = L1
fudge = 0.000001;

  dist_ij = sqrt((d_ij.^2)*a);
  deri1_d_ij = 0.5*(d_ij.^2)/(dist_ij + (dist_ij==0)*fudge);
  deri2_d_ij = -0.25*(d_ij.^2)'*(d_ij.^2)/(dist_ij^3 + (dist_ij==0)*fudge);


function [dist_ij, deri1_d_ij, deri2_d_ij] = distance2(a, d_ij)
% distance and derivatives of distance using distance1: distance(d) = sqrt(L1)
fudge = 0.000001;

  dist_ij = ((d_ij.^2)*a)^(1/4);
  deri1_d_ij = 0.25*(d_ij.^2)/(dist_ij^3 + (dist_ij==0)*fudge);
  deri2_d_ij = -0.25*0.75*(d_ij.^2)'*(d_ij.^2)/(dist_ij^7+(dist_ij==0)*fudge);


function [dist_ij, deri1_d_ij, deri2_d_ij] = distance3(a, d_ij)
% distance and derivative of distance using distance3: 1-exp(-\beta*L1)
fudge = 0.000001;

  beta = 0.5;
  M2_ij = (d_ij.^2)'*(d_ij.^2);
  L1 = sqrt((d_ij.^2)*a);
  dist_ij = 1 - exp(-beta*L1);
  deri1_d_ij = 0.5*beta*exp(-beta*L1)*(d_ij.^2)/(L1+(L1==0)*fudge);
  deri2_d_ij = -0.25*beta^2*exp(-beta*L1)*M2_ij/(L1^2+(L1==0)*fudge) - ...
               0.25*beta*exp(-beta*L1)*M2_ij/(L1^3+(L1==0)*fudge);
