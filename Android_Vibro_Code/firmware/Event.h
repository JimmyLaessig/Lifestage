#ifndef EVENT_H
#define EVENT_H

class Event
{
public:
	Event(int acId, int inten, int tinte, int dura, int pa, int pb);
	Event();
	int acId;
	int inten;
	int tinte;
	int dura;
	int pa;
	int pb;
};
#endif