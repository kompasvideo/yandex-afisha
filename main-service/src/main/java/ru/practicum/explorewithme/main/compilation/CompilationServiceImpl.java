package ru.practicum.explorewithme.main.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.main.category.CategoryRepository;
import ru.practicum.explorewithme.main.category.model.Category;
import ru.practicum.explorewithme.main.compilation.model.Compilation;
import ru.practicum.explorewithme.main.compilation.model.CompilationDto;
import ru.practicum.explorewithme.main.compilation.model.EventCompilation;
import ru.practicum.explorewithme.main.event.EventRepository;
import ru.practicum.explorewithme.main.event.model.Event;
import ru.practicum.explorewithme.main.event.model.EventShortDto;
import ru.practicum.explorewithme.main.exception.NotFoundException;
import ru.practicum.explorewithme.main.exception.ValidationException;
import ru.practicum.explorewithme.main.mappers.MapperCompilation;
import ru.practicum.explorewithme.main.mappers.MapperEvent;
import ru.practicum.explorewithme.main.user.UserRepository;
import ru.practicum.explorewithme.main.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationEventRepository compilationEventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Compilation addCompilation(CompilationDto compilationDto) {
        List<Long> events = compilationDto.getEvents();

        for (Long event : events) {
            if (!eventRepository.existsById(event))
                throw new NotFoundException("NotFoundException CompilationServiceImpl addCompilation, eventId"
                    + event);
        }

        Compilation compilation = compilationRepository.save(Compilation.builder()
            .pinned(compilationDto.getPinned())
            .title(compilationDto.getTitle())
            .build());

        List<EventCompilation> eventCompilations = events.stream()
            .map(event -> MapperCompilation.mapEventCompilation(event, compilation.getId()))
            .collect(Collectors.toList());

        compilationEventRepository.saveAll(eventCompilations);

        List<EventShortDto> eventShortDtoList = eventRepository.findAllById(events)
            .stream()
            .map(event -> mapEventToShortDto(event))
            .collect(Collectors.toList());

        compilation.setEvents(eventShortDtoList);

        return Compilation.builder()
            .id(compilation.getId())
            .pinned(compilation.getPinned())
            .title(compilation.getTitle())
            .events(eventShortDtoList)
            .build();
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl deleteCompilation, compId: "
                + compId);

        compilationEventRepository.deleteAllById(compilationEventRepository.findAllByCompilationId(compId));
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public void deleteById(Long compId, Long eventId) {
        if (!eventRepository.existsById(eventId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl deleteById, eventId: " + eventId);
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl deleteById, compId: "
                + compId);

        if (!compilationEventRepository.existsByEventIdAndCompilationId(eventId, compId)) {
            throw new NotFoundException("???? ?????????????? ?????????????? ?? ????????????????");
        }

        Long id = compilationEventRepository.findEventIdByEventIdAndCompilationId(eventId, compId);

        compilationEventRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addEvent(Long compId, Long eventId) {
        if (!eventRepository.existsById(eventId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl addEvent, eventId: " + eventId);
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl addEvent, compId: "
                + compId);

        if (compilationEventRepository.existsByEventIdAndCompilationId(eventId, compId)) {
            throw new NotFoundException("?????????????? ?????? ?????????????????? ?? ????????????????");
        }

        compilationEventRepository.save(MapperCompilation.mapEventCompilation(eventId, compId));
    }

    @Override
    @Transactional
    public void unpin(Long compId) {
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl unpin, compId: "
                + compId);
        Compilation compilationDto = compilationRepository.getReferenceById(compId);

        if (!compilationDto.getPinned()) {
            throw new ValidationException("???????????????? ???? ???????????????????? ???? ?????????????? ????????????????");
        }

        compilationDto.setPinned(false);
        compilationRepository.save(compilationDto);
    }

    @Override
    @Transactional
    public void pin(Long compId) {
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl pin, compId: "
                + compId);
        Compilation compilationDto = compilationRepository.getReferenceById(compId);

        if (compilationDto.getPinned()) {
            throw new ValidationException("???????????????? ?????? ???????????????????? ???? ?????????????? ????????????????");
        }

        compilationDto.setPinned(true);
        compilationRepository.save(compilationDto);
    }

    @Override
    public List<Compilation> findAll(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilationDtoPage;

        if (pinned != null) {
            compilationDtoPage = compilationRepository
                .findAllByPinned(pinned, PageRequest.of(from, size));
        } else {
            compilationDtoPage = compilationRepository.findAll(PageRequest.of(from, size));
        }

        List<Compilation> compilationDtoList = compilationDtoPage.stream()
            .peek(compilation -> fillCompilation(compilation))
            .collect(Collectors.toList());

        if (compilationDtoList.isEmpty()) {
            throw new NotFoundException("???????????????? ?????????????? ???? ??????????????");
        }

        return compilationDtoList;
    }

    @Override
    public Compilation findById(Long compId) {
        if (!compilationRepository.existsById(compId))
            throw new NotFoundException("NotFoundException CompilationServiceImpl findById, compId: "
                + compId);
        Compilation compilationDto = compilationRepository.findCompilationById(compId);
        fillCompilation(compilationDto);

        return compilationDto;
    }

    private EventShortDto mapEventToShortDto(Event event) {
        Long id = event.getCategory();
        Category category = categoryRepository.findById(id).orElseThrow(() ->
            new NotFoundException("NotFoundException CompilationServiceImpl EventShortDto, id: "
                + id));
        User user = userRepository.getReferenceById(event.getInitiator());

        return MapperEvent.mapEventToShortDto(event, category, user);
    }

    private void fillCompilation(Compilation compilation) {
        List<Long> eventsIds = compilationEventRepository.findAllByCompilationId(compilation.getId());
        List<EventShortDto> eventShortDtoList = eventRepository.findAllById(eventsIds)
            .stream()
            .map(event -> mapEventToShortDto(event))
            .collect(Collectors.toList());

        compilation.setEvents(eventShortDtoList);
    }
}
