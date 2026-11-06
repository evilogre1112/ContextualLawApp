"""Inference-only XLM-RoBERTa + CRF compatible with PhoBERT-Legal-NER checkpoints.

The training implementation uses `roberta`, `classifier`, `crf` and optionally
`projection_head` as state_dict prefixes. The projection head is never needed
for inference, but must be instantiated for a strict checkpoint load.
"""

import torch
from torch import nn
from torchcrf import CRF
from transformers import XLMRobertaForTokenClassification


class ProjectionHead(nn.Module):
    def __init__(self, input_dim: int):
        super().__init__()
        self.mlp = nn.Sequential(
            nn.Linear(input_dim, 256),
            nn.ReLU(),
            nn.Linear(256, 128),
        )


class XlmRobertaCrf(XLMRobertaForTokenClassification):
    def __init__(self, config, *, has_projection_head: bool = False):
        super().__init__(config=config)
        self.crf = CRF(config.num_labels, batch_first=True)
        self.projection_head = (ProjectionHead(config.hidden_size)
                                if has_projection_head else None)

    def forward(self, input_ids, attention_mask, valid_ids, label_masks):
        hidden = self.roberta(input_ids=input_ids, attention_mask=attention_mask)[0]
        rows = torch.arange(hidden.size(0), device=hidden.device).unsqueeze(1)
        word_hidden = hidden[rows, valid_ids]
        emissions = self.classifier(self.dropout(word_hidden)).float()
        return self.crf.decode(emissions, mask=label_masks.bool())
