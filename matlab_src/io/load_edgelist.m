% Bryan Perozzi

function [A labels] = load_edgelist (filename, mapping)
  
     if nargin < 2
         no_mapping =   1;
     else
         no_mapping = 0;
     end

%     M = dlmread(filename, ' ');

    fid = fopen(filename);   
    M = textscan(fid, '%s %s');    
    fclose(fid);
  
    M = [M{1} M{2}];
       
%   relabel vertices

    if no_mapping
        mapping = unique(sort([M(:,1) M(:,2)]));
    end
    
    
    [~, edges] = ismember(M, mapping);
    
%     for i=1:numel(M)
% %          M(i) = find(mapping == M(i));
% %         edges(i) = find(ismember(mapping, M(i)));
%         [~, edges(i)] = ismember(M(i), mapping);
%     end    
    
    elements = numel(mapping);
    
    A = sparse(edges(:,1), edges(:,2), ones(size(edges,1),1), elements, elements); 
end
