function [WeightedA] = focusco_main(graph_file, data_file, similar_nodes_file, varargin)
   
   addpath('matlab_src/PGDM')
   addpath('matlab_src/io')
   addpath('matlab_src')

   % open and load files
   X = load(data_file);
   A = load_edgelist(graph_file);     
   similar_pairs = load(similar_nodes_file);
   
   % pull out some useful variables
   num_vertices = size(A,1);
   
   % parse remaining arguments
   p = inputParser;
   defaultGamma = 1;
   defaultDissimilarSamples = 2*size(similar_pairs,2);
   default_topk_features = size(X,2);   
   default_dml = 'sparse';
   default_file_out = '';
   default_reweight_type = 'sparse';
   
   addOptional(p, 'gamma', defaultGamma,@isnumeric);
   addOptional(p, 'size_D', defaultDissimilarSamples,@isnumeric);
   addOptional(p, 'top_k_features', default_topk_features, @isnumeric);   
   addOptional(p, 'dml_datatype', default_dml, @(x) strcmp(x, 'sparse') || strcmp(x, 'dense'));   
   addOptional(p, 'file_output', default_file_out, @isstr);   
   addOptional(p, 'reweight_type', default_reweight_type, @isstr);   

   parse(p, varargin{:});
   
   gamma = p.Results.gamma;
   num_dissimilar_pairs = p.Results.size_D;
   top_k_features = p.Results.top_k_features;
   dml_datatype = p.Results.dml_datatype;
   dm_file_out = p.Results.file_output;
   reweight_type = p.Results.reweight_type;
   
   fprintf('FocusCO Distance Metric Learning\n-------------------------------------\n')
   fprintf('Gamma: %f\n', gamma)
   fprintf('# Dissimilar pairs: %d\n', num_dissimilar_pairs)
   fprintf('Distance Metric Learning Data Type: %s\n', dml_datatype)   
   fprintf('# Features to consider (if dml_datatype == sparse): %d\n', top_k_features)
   fprintf('Type of graph reweighting (sparse or dense similarity): %s\n', reweight_type)
   fprintf('Graph Output File: %s\n\n', dm_file_out)
   
   % use dense or sparse DML?
   [ DM, S, D ] = distance_metric_learning_manual(X, similar_pairs, num_dissimilar_pairs, num_vertices , gamma, top_k_features, dml_datatype);
   
   fprintf('Distance metric:\n');
   DM
   
   if strcmp(reweight_type, 'sparse')
        WeightedA = reweigh_sparse(A, X, DM);
   else       
        WeightedA = reweigh(A, X, DM);
   end
   
   if ~strcmp(dm_file_out, '')
        savesparse(dm_file_out, WeightedA);
   end
   %exit    
end
