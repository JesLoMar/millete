export interface WikiTopic {
  title: string;
  content: string;
  image?: string | null;
}

export interface WikiSection {
  title: string;
  description: string;
  topics: WikiTopic[];
}

export interface WikiSections {
  sections: Record<string, WikiSection>;
}